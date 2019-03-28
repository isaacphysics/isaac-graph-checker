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
package org.isaacphysics.graphchecker.geometry;

import org.isaacphysics.graphchecker.TestHelpers;
import org.isaacphysics.graphchecker.data.Point;
import org.junit.Test;
import org.isaacphysics.graphchecker.data.Line;

import java.util.Collections;
import java.util.List;

import static org.junit.Assert.*;

public class LinesTest {

    @Test
    public void splitOnPointsOfInterest() {
        Line line = TestHelpers.lineOf(-10,0, -5,5, 0,0, 5,-5, 10,0);

        List<Line> lines = Lines.splitOnPoints(line, line.getPointsOfInterest());

        assertEquals(3, lines.size());
        assertEquals(TestHelpers.lineOf(-10,0, -5,5), lines.get(0));
        assertEquals(TestHelpers.lineOf(-5,5, 0,0, 5,-5), lines.get(1));
        assertEquals(TestHelpers.lineOf(5,-5, 10, 0), lines.get(2));
    }

    @Test
    public void getMedianCentreOfLine() {
        Line line = TestHelpers.lineOf(-10,0, -5,5, 5,-5, 10,0);

        Point centre = Lines.getCentreOfPoints(line.getPoints());

        assertEquals(new Point(0, 0), centre);
    }

    @Test
    public void getSizeOfEmptyLine() {
        Line line = TestHelpers.lineOf(new Point[]{});

        Point size = Lines.getSize(line);

        assertEquals(new Point(0, 0), size);
    }
    @Test
    public void testSimpleIntersections() {
        Line lineA = TestHelpers.lineOf(0,0, 10,10);
        Line lineB = TestHelpers.lineOf(10,0, 0,10);

        List<Point> intersections = Lines.findIntersections(lineA, lineB);

        assertEquals(Collections.singletonList(new Point(5, 5)), intersections);
    }

    @Test
    public void testParallelNonIntersection() {
        Line lineA = TestHelpers.lineOf(0,0, 10,10);
        Line lineB = TestHelpers.lineOf(1,1, 11,11);

        List<Point> intersections = Lines.findIntersections(lineA, lineB);

        assertEquals(Collections.emptyList(), intersections);
    }

    @Test
    public void testNonIntersection() {
        Line lineA = TestHelpers.lineOf(0,20, 10,10);
        Line lineB = TestHelpers.lineOf(0,-20, 10,-10);

        List<Point> intersections = Lines.findIntersections(lineA, lineB);

        assertEquals(Collections.emptyList(), intersections);
    }

    @Test
    public void testMediumIntersections() {
        Line lineA = TestHelpers.lineOf(0,0, 5,5, 10,10);
        Line lineB = TestHelpers.lineOf(10,0, 0,10);

        List<Point> intersections = Lines.findIntersections(lineA, lineB);

        assertEquals(Collections.singletonList(new Point(5, 5)), intersections);
    }

    @Test
    public void testComplexIntersections() {
        Line lineA = TestHelpers.lineOf(Math::sin, -10, 10);
        Line lineB = TestHelpers.lineOf(Math::cos, -10, 10);

        List<Point> intersections = Lines.findIntersections(lineA, lineB);

        assertEquals(6, intersections.size());
    }
}