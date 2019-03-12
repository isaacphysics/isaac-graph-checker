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

import uk.ac.cam.cl.dtg.isaac.graphmarker.TestHelpers;
import org.junit.Test;
import uk.ac.cam.cl.dtg.isaac.graphmarker.data.Line;
import uk.ac.cam.cl.dtg.isaac.graphmarker.data.Point;
import uk.ac.cam.cl.dtg.isaac.graphmarker.geometry.Sector;

import java.util.Arrays;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import static org.junit.Assert.*;
import static uk.ac.cam.cl.dtg.isaac.graphmarker.geometry.Sector.*;
import static uk.ac.cam.cl.dtg.isaac.graphmarker.TestHelpers.lineOf;

public class ExpectedSectorsFeatureTest {

    @Test
    public void basicLineHasCorrectSectorList() {
        List<Sector> sectorList =  ExpectedSectorsFeature.manager.convertLineToSectorList(TestHelpers.lineOf(
                new Point(-1, 1),
                new Point(2, -1)
        ));

        assertEquals("[topLeft, +Yaxis, topRight, +Xaxis, bottomRight]", sectorList.toString());
    }

    @Test
    public void xSquaredMinusTwoHasCorrectSectorList() {
        Predicate<Line> testFeature = line -> ExpectedSectorsFeature.manager.deserialize(
            "topLeft, onAxisWithNegativeX, bottomLeft, onAxisWithNegativeY, bottomRight, onAxisWithPositiveX, topRight").match(line);

        assertTrue(testFeature.test(TestHelpers.lineOf(x -> x*x - 2, -5, 5)));
        assertFalse(testFeature.test(TestHelpers.lineOf(x -> x*x + 2, -5, 5)));
    }

    @Test
    public void xCubedPlusSquaredMinusTwoHasCorrectSectorList() {
        Predicate<Line> testFeature = line -> ExpectedSectorsFeature.manager.deserialize(
            "bottomLeft, onAxisWithNegativeX, topLeft, onAxisWithPositiveY, topRight, onAxisWithPositiveX, bottomRight, onAxisWithPositiveX, topRight").match(line);

        assertTrue(testFeature.test(TestHelpers.lineOf(x -> x*x*x - 3*x*x + 2, -10, 10)));
        assertFalse(testFeature.test(TestHelpers.lineOf(x -> x*x - 3*x*x + 2, -10, 10)));
    }

    @Test
    public void cosXFromMinus2PiTo2Pi() {
        Predicate<Line> testFeature = line -> ExpectedSectorsFeature.manager.deserialize(
            "topLeft, onAxisWithNegativeX, bottomLeft, onAxisWithNegativeX, topLeft, onAxisWithPositiveY, topRight, onAxisWithPositiveX, bottomRight, onAxisWithPositiveX, topRight").match(line);

        assertTrue(testFeature.test(TestHelpers.lineOf(x -> Math.cos(x), -2 * Math.PI, 2 * Math.PI)));
        assertFalse(testFeature.test(TestHelpers.lineOf(x -> Math.cos(x + Math.PI / 2), -2 * Math.PI, 2 * Math.PI)));
    }

    @Test
    public void sinXFromMinus2PiTo2Pi() {
        Predicate<Line> testFeature = line -> ExpectedSectorsFeature.manager.deserialize(
            "onAxisWithNegativeX, topLeft, onAxisWithNegativeX, bottomLeft, origin, topRight, onAxisWithPositiveX, bottomRight, onAxisWithPositiveX").match(line);

        assertTrue(testFeature.test(TestHelpers.lineOf(x -> Math.sin(x), -2 * Math.PI, 2 * Math.PI)));
        assertFalse(testFeature.test(TestHelpers.lineOf(x -> Math.cos(x), -2 * Math.PI, 2 * Math.PI)));
    }

    @Test
    public void matchCorrectlyWhenStartingAtOrigin() {
        Predicate<Line> testFeature = line -> ExpectedSectorsFeature.manager.deserialize("origin, topRight").match(line);

        assertTrue(testFeature.test(TestHelpers.lineOf(x -> x, 0, 10)));
    }

    @Test
    public void customSectionListMatches() {
        Sector[] sectors = {Sector.topLeft, Sector.bottomRight};
        ExpectedSectorsFeature feature = new ExpectedSectorsFeature(Arrays.asList(sectors));

        Predicate<Line> testFeature = line -> feature.deserialize("topLeft, bottomRight").match(line);

        assertTrue(testFeature.test(TestHelpers.lineOf(x -> 2 - x, -5, 5)));
    }

    @Test
    public void generateMatchesItself() {
        List<String> data = ExpectedSectorsFeature.manager.generate((TestHelpers.lineOf(x -> Math.cos(x), -2 * Math.PI, 2 * Math.PI)));

        Predicate<Line> testFeature = line -> ExpectedSectorsFeature.manager.deserialize(data.get(0)).match(line);

        assertTrue(testFeature.test(wobbly(TestHelpers.lineOf(x -> Math.cos(x), -2 * Math.PI, 2 * Math.PI))));
    }

    private Line wobbly(Line points) {
        return lineOf(points.stream()
                .map(p -> new Point(
                        p.getX() + Math.random() * 0.02 - 0.01,
                        p.getY() + Math.random() * 0.02 - 0.01))
                .collect(Collectors.toList()));
    }

    @Test
    public void twoXminusThreeCanPassNearOrigin() {
        // Due to the scaling of everything to -1 to 1, a correct answer can be arbitrarily close to the origin

        // TODO: How arbitrarily close to the origin can we go?

        Predicate<Line> testFeature = line -> ExpectedSectorsFeature.manager.deserialize(
            "bottomLeft, -Xaxis, topLeft, +Yaxis, topRight").match(line);

        assertTrue(testFeature.test(TestHelpers.lineOf(x -> 2 * x + 0.03, -0.1, 0.1)));
    }

    @Test
    public void crossingTheAxisShouldBeIrreversible() {
        Predicate<Line> testFeature = line -> ExpectedSectorsFeature.manager.deserialize(
            "bottomRight,+Xaxis,topRight,+Xaxis,bottomRight,+Xaxis,topRight").match(line);

        assertTrue(testFeature.test(TestHelpers.lineOf(x -> (x - 1) * (x - 3) * (x - 4), 0.5, 10)));

        // Shift graph up so the crossing back into the bottomRight doesn't happen properly
        assertFalse(testFeature.test(TestHelpers.lineOf(x -> (x - 1) * (x - 3) * (x - 4) + 0.64, 0.5, 10)));

        double minimaX = (8 + Math.sqrt(7)) / 3;
        double minimaY = (minimaX - 1) * (minimaX - 3) * (minimaX - 4);

        // But only just going over the axis should still be allowed
        assertTrue(testFeature.test(TestHelpers.lineOf(x -> (x - 1) * (x - 3) * (x - 4) - minimaY - (AXIS_SLOP / 4), 0.5, 10)));
    }

}