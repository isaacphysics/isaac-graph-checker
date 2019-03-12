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
package uk.ac.cam.cl.dtg.isaac.graphmarker.geometry;

import org.junit.Test;
import uk.ac.cam.cl.dtg.isaac.graphmarker.TestHelpers;
import uk.ac.cam.cl.dtg.isaac.graphmarker.data.Intersection;
import uk.ac.cam.cl.dtg.isaac.graphmarker.data.Point;

import java.util.Arrays;

import static org.junit.Assert.*;

public class SectorTest {

    private void assertInside(Intersection c) {
        assertEquals(Intersection.INSIDE, c);
    }

    private void assertIntersects(Intersection c) {
        assertEquals(Intersection.INTERSECTS, c);
    }

    private void assertOutside(Intersection c) {
        assertEquals(Intersection.OUTSIDE, c);
    }

    private static final Point x1y1 = new Point(1, 1);
    private static final Point x_1y_1 = new Point(-1, -1);
    private static final Point x1y_1 = new Point(1, -1);
    private static final Point x_1y1 = new Point(-1, 1);
    private static final Point x0y0 = new Point(0, 0);
    private static final Point x0y1 = new Point(0, 1);
    private static final Point x0y_1 = new Point(0, -1);


    @Test
    public void topRightWorksOnPoint() {
        Sector topRight = Sector.topRight;

        assertTrue(topRight.contains(x1y1));
        assertFalse(topRight.contains(x_1y1));
        assertFalse(topRight.contains(x_1y_1));
        assertFalse(topRight.contains(x1y_1));
    }

    @Test
    public void topRightWorksOnLineFullyInside() {
        Sector topRight = Sector.topRight;

        assertInside(topRight.intersects(TestHelpers.lineOf(x1y1, new Point(2, 1))));
    }

    @Test
    public void topRightWorksOnLineFromOrigin() {
        Sector topRight = Sector.topRight;

        assertIntersects(topRight.intersects(TestHelpers.lineOf(x1y1, x0y0)));
    }

    @Test
    public void topRightWorksOnLineFromBottomLeft() {
        Sector topRight = Sector.topRight;

        assertIntersects(topRight.intersects(TestHelpers.lineOf(x1y1, x_1y_1)));
    }

    @Test
    public void topRightWorksOnLineFromTopLeftToBottomRight() {
        Sector topRight = Sector.topRight;

        assertIntersects(topRight.intersects(TestHelpers.lineOf(x_1y1, new Point(2, -1))));
    }

    @Test
    public void topLeftWorks() {
        Sector topLeft = Sector.topLeft;

        assertIntersects(topLeft.intersects(TestHelpers.lineOf(x_1y1, x_1y_1)));
    }

    @Test
    public void bottomLeftWorks() {
        Sector bottomLeft = Sector.bottomLeft;

        assertInside(bottomLeft.intersects(TestHelpers.lineOf(x_1y_1, new Point(-2, -1))));
    }

    @Test
    public void bottomRightWorks() {
        Sector bottomRight = Sector.bottomRight;

        assertIntersects(bottomRight.intersects(TestHelpers.lineOf(x_1y_1, new Point(2, 1))));
    }


    @Test
    public void positiveXaxisWorks() {
        Sector axis = Sector.onAxisWithPositiveY;
        assertInside(axis.intersects(TestHelpers.lineOf(new Point(0, 0.0001), x0y1)));

        assertIntersects(axis.intersects(TestHelpers.lineOf(x_1y1, x1y1)));

        assertOutside(axis.intersects(TestHelpers.lineOf(x_1y_1, x1y_1)));

        assertOutside(axis.intersects(TestHelpers.lineOf(x_1y_1, new Point(0, -0.0001))));
    }

    @Test
    public void axesHaveCorrectPointHandling() {
        Sector[] axes = new Sector[] {
                Sector.byName("+Yaxis"),
                Sector.byName("-Xaxis"),
                Sector.byName("-Yaxis"),
                Sector.byName("+Xaxis")
        };

        Point[] outsidePoints = new Point[] {
                x_1y_1,
                x0y_1,
                new Point(0, -0.0001),
        };

        Point[] insidePoints = new Point[] {
                x0y1,
                new Point(-0.005, 0.0001),
                new Point(+0.005, 0.0001),
                new Point(0, 1e10),
                x0y0,
        };

        for (Sector axis : axes) {
            for (Point p : outsidePoints) {
                assertFalse(axis.contains(p));
            }
            for (Point p : insidePoints) {
                assertTrue(axis.contains(p));
            }
            // Rotate points by 90 degrees
            outsidePoints = Arrays.stream(outsidePoints).map(p -> new Point(-p.getY(), p.getX())).toArray(Point[]::new);
            insidePoints = Arrays.stream(insidePoints).map(p -> new Point(-p.getY(), p.getX())).toArray(Point[]::new);
        }
    }

    @Test
    public void testOrigin() {
        Sector origin = Sector.origin;

        assertTrue(origin.contains(new Point(0.005, -0.005)));
        assertFalse(origin.contains(new Point(-0.1, 0)));
        assertFalse(origin.contains(new Point(0.1, 0.1)));
        assertFalse(origin.contains(new Point(0, 0.1)));

        assertInside(origin.intersects(TestHelpers.lineOf(new Point(0, 0))));

        assertIntersects(origin.intersects(TestHelpers.lineOf(new Point(-1, 0), new Point(100, 1))));

        assertOutside(origin.intersects(TestHelpers.lineOf(new Point(-0.5, 0), new Point(1, 0.2))));
    }

    @Test
    public void quadrantsHaveCorrectPointHandling() {
        Sector[] quadrants = new Sector[] {
                Sector.topRight,
                Sector.topLeft,
                Sector.bottomLeft,
                Sector.bottomRight
        };

        Point[] outsidePoints = new Point[] {
                x_1y_1,
                x0y_1,
                new Point(1, -0.0001),
        };

        Point[] insidePoints = new Point[] {
                x1y1,
                new Point(+0.005, 0.0001),
                new Point(+0.005, 10),
                new Point(100, 0.01),
                x0y0,
        };

        for (Sector quadrant : quadrants) {
            for (Point p : outsidePoints) {
                assertFalse(quadrant + " should not contain " + p, quadrant.contains(p));
            }
            for (Point p : insidePoints) {
                assertTrue(quadrant + " should contain " + p, quadrant.contains(p));
            }
            // Rotate points by 90 degrees
            outsidePoints = Arrays.stream(outsidePoints).map(p -> new Point(-p.getY(), p.getX())).toArray(Point[]::new);
            insidePoints = Arrays.stream(insidePoints).map(p -> new Point(-p.getY(), p.getX())).toArray(Point[]::new);
        }
    }

    @Test(expected = IllegalArgumentException.class)
    public void unknownSectorNameThrowsAnError() {
        Sector.byName("foo!+~~");
    }
}