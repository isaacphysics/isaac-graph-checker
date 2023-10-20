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
import org.isaacphysics.graphchecker.data.Point;
import org.isaacphysics.graphchecker.geometry.SectorBuilder;
import org.isaacphysics.graphchecker.geometry.SectorClassifier;
import org.isaacphysics.graphchecker.settings.SettingsWrapper;
import org.junit.Test;
import org.isaacphysics.graphchecker.data.Line;
import org.isaacphysics.graphchecker.geometry.Sector;

import java.util.Arrays;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import static org.junit.Assert.*;
import static org.isaacphysics.graphchecker.TestHelpers.lineOf;

public class ExpectedSectorsFeatureTest {

    private final ExpectedSectorsFeature expectedSectorsFeature = new ExpectedSectorsFeature(SettingsWrapper.DEFAULT);

    @Test
    public void basicLineHasCorrectSectorList() {
        List<Sector> sectorList =  expectedSectorsFeature.convertLineToSectorList(TestHelpers.lineOf(
                new Point(-1, 1),
                new Point(2, -1)
        ));

        assertEquals("[topLeft, +Yaxis, topRight, +Xaxis, bottomRight]", sectorList.toString());
    }

    @Test
    public void xSquaredMinusTwoHasCorrectSectorList() {
        Predicate<Line> testFeature = expectedSectorsFeature.deserializeInternal(
            "topLeft, -Xaxis, bottomLeft, -Yaxis, bottomRight, +Xaxis, topRight");

        assertTrue(testFeature.test(TestHelpers.lineOf(x -> x*x - 2, -5, 5)));
        assertFalse(testFeature.test(TestHelpers.lineOf(x -> x*x + 2, -5, 5)));
    }

    @Test
    public void xCubedPlusSquaredMinusTwoHasCorrectSectorList() {
        Predicate<Line> testFeature = expectedSectorsFeature.deserializeInternal(
            "bottomLeft, -Xaxis, topLeft, +Yaxis, topRight, +Xaxis, bottomRight, +Xaxis, topRight");

        assertTrue(testFeature.test(TestHelpers.lineOf(x -> x*x*x - 3*x*x + 2, -10, 10)));
        assertFalse(testFeature.test(TestHelpers.lineOf(x -> x*x - 3*x*x + 2, -10, 10)));
    }

    @Test
    public void cosXFromMinus2PiTo2Pi() {
        Predicate<Line> testFeature = expectedSectorsFeature.deserializeInternal(
            "topLeft, -Xaxis, bottomLeft, -Xaxis, topLeft, +Yaxis, topRight, +Xaxis, bottomRight, +Xaxis, topRight");

        assertTrue(testFeature.test(TestHelpers.lineOf(Math::cos, -2 * Math.PI, 2 * Math.PI)));
        assertFalse(testFeature.test(TestHelpers.lineOf(x -> Math.cos(x + Math.PI / 2), -2 * Math.PI, 2 * Math.PI)));
    }

    @Test
    public void sinXFromMinus2PiTo2Pi() {
        Predicate<Line> testFeature = expectedSectorsFeature.deserializeInternal(
            "-Xaxis, topLeft, -Xaxis, bottomLeft, origin, topRight, +Xaxis, bottomRight, +Xaxis");

        assertTrue(testFeature.test(TestHelpers.lineOf(Math::sin, -2 * Math.PI, 2 * Math.PI)));
        assertFalse(testFeature.test(TestHelpers.lineOf(Math::cos, -2 * Math.PI, 2 * Math.PI)));
    }

    @Test
    public void matchCorrectlyWhenStartingAtOrigin() {
        Predicate<Line> testFeature = expectedSectorsFeature.deserializeInternal("origin, topRight");

        assertTrue(testFeature.test(TestHelpers.lineOf(x -> x, 0, 10)));
    }

    @Test
    public void customSectionListMatches() {
        SectorClassifier.Settings settings = new SectorClassifier.Settings() {
            public List<Sector> getOrderedSectors() {
                SectorBuilder sectorBuilder = getSectorBuilder();
                Sector[] sectors = {sectorBuilder.byName(SectorBuilder.TOP_LEFT), sectorBuilder.byName(SectorBuilder.BOTTOM_RIGHT)};
                return Arrays.asList(sectors);
            }
        };

        ExpectedSectorsFeature feature = new ExpectedSectorsFeature(settings);

        Predicate<Line> testFeature = feature.deserializeInternal("topLeft, bottomRight");

        assertTrue(testFeature.test(TestHelpers.lineOf(x -> 2 - x, -5, 5)));
    }

    @Test
    public void generateMatchesItself() {
        List<String> data = expectedSectorsFeature.generate((TestHelpers.lineOf(Math::cos, -2 * Math.PI, 2 * Math.PI)));

        Predicate<Line> testFeature = expectedSectorsFeature.deserializeInternal(data.get(0));

        assertTrue(testFeature.test(wobbly(TestHelpers.lineOf(Math::cos, -2 * Math.PI, 2 * Math.PI))));
    }

    private Line wobbly(Line points) {
        return lineOf(points.stream()
                .map(p -> new Point(
                        p.getX() + Math.random() * 0.02 - 0.01,
                        p.getY() + Math.random() * 0.02 - 0.01))
                .collect(Collectors.toList()));
    }

    @Test
    public void twoXPlusNoughtPointOneCanPassNearOrigin() {
        // How arbitrarily close to the origin can we go?
        // Not as close as 2x + 0.03 (origin slop is 0.05 for these tests)

        Predicate<Line> testFeature = expectedSectorsFeature.deserializeInternal(
            "bottomLeft, -Xaxis, topLeft, +Yaxis, topRight");

        assertTrue(testFeature.test(TestHelpers.lineOf(x -> 2 * x + 0.1, -0.1, 0.1)));
    }

    @Test
    public void crossingTheAxisShouldBeIrreversible() {
        Predicate<Line> testFeature = expectedSectorsFeature.deserializeInternal(
            "bottomRight,+Xaxis,topRight,+Xaxis,bottomRight,+Xaxis,topRight");

        double axisSlop = SettingsWrapper.DEFAULT.getAxisSlop();
        // Should calculate this from the function but this is a quick fix
        double maxFunctionValue = (6 - 1)*(6 - 3)*(6 - 5);
        // need to unnormalise the slop
        double adjustedSlop = (axisSlop * maxFunctionValue) + 0.0001;

        double minimaX = (9 + (2*Math.sqrt(3))) / 3;
        double minimaY = (minimaX - 1) * (minimaX - 3) * (minimaX - 5);
        // So that the adjusted curve is not exactly on the line
        double minimaYSloppy = minimaY - 0.01;

        // New function so that maxima - minima is outside the axisSlop range
        assertTrue(testFeature.test(TestHelpers.lineOf(x -> (x - 1) * (x - 3) * (x - 5), 0.5, 6)));

        assertFalse(testFeature.test(TestHelpers.lineOf(x -> (x - 1) * (x - 3) * (x - 5) - minimaYSloppy + adjustedSlop, 0.5, 6)));

        // But only just going over the axis (including the slop) should still be allowed
        assertTrue(testFeature.test(TestHelpers.lineOf(x -> (x - 1) * (x - 3) * (x - 5) - minimaY - adjustedSlop, 0.5, 6)));
    }

    @Test(timeout=1000)
    public void checkPerformance() {
        double size = 50 * Math.PI / 2;
        Line upAndDown = TestHelpers.lineOf(Math::sin, -size, size);
        Line downAndUp = TestHelpers.lineOf(x -> -Math.sin(x), -size, size);
        Line shifted = TestHelpers.lineOf(x -> -Math.cos(x), -size, size);

        List<String> features = expectedSectorsFeature.generate(upAndDown);

        String feature = features.get(0);

        Predicate<Line> testFeature = expectedSectorsFeature.deserializeInternal(feature);

        assertTrue(testFeature.test(upAndDown));
        assertFalse(testFeature.test(downAndUp));
        assertFalse(testFeature.test(shifted));
    }

}