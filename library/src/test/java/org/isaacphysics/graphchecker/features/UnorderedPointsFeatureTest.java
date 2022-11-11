package org.isaacphysics.graphchecker.features;

import org.isaacphysics.graphchecker.TestHelpers;
import org.isaacphysics.graphchecker.data.Line;
import org.isaacphysics.graphchecker.settings.SettingsWrapper;
import org.junit.Test;

import java.util.List;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class UnorderedPointsFeatureTest {

    private final UnorderedPointsFeature unorderedPointsFeature = new UnorderedPointsFeature(SettingsWrapper.DEFAULT);

    @Test
    public void pointsFeature_curveWithPOIsMatchingSpec_matches(){
        // Arrange
        Line sine = TestHelpers.lineOf(Math::sin, -Math.PI, Math.PI);
        String spec = "minima in bottomLeft, maxima in topRight";

        // Act
        boolean matches = unorderedPointsFeature.deserializeInternal(spec).test(sine);

        // Assert
        assertTrue(matches);
    }

    @Test
    public void pointsFeature_curveWithPOIsMatchingAnySectorSpec_matches(){
        // Arrange
        Line sine = TestHelpers.lineOf(Math::sin, -Math.PI, Math.PI);
        String spec = "minima in any, maxima in any";

        // Act
        boolean matches = unorderedPointsFeature.deserializeInternal(spec).test(sine);

        // Assert
        assertTrue(matches);
    }

    @Test
    public void pointsFeature_curveWithPOIsMatchingPartialAnySectorSpec_matches(){
        // Arrange
        Line sine = TestHelpers.lineOf(Math::sin, -Math.PI, Math.PI);
        String spec = "minima in any, maxima in topRight";

        // Act
        boolean matches = unorderedPointsFeature.deserializeInternal(spec).test(sine);

        // Assert
        assertTrue(matches);
    }

    @Test
    public void pointsFeature_curveWithOutOfOrderPOIs_matches(){
        // Arrange
        Line sine = TestHelpers.lineOf(Math::sin, -Math.PI, Math.PI);
        String spec = "maxima in topRight, minima in bottomLeft";

        // Act
        boolean matches = unorderedPointsFeature.deserializeInternal(spec).test(sine);

        // Assert
        assertTrue(matches);
    }

    @Test
    public void pointsFeature_curveWithOutOfOrderPOIsAnySectorSpec_matches(){
        // Arrange
        Line sine = TestHelpers.lineOf(Math::sin, -Math.PI, Math.PI);
        String spec = "maxima in any, minima in any";

        // Act
        boolean matches = unorderedPointsFeature.deserializeInternal(spec).test(sine);

        // Assert
        assertTrue(matches);
    }
}
