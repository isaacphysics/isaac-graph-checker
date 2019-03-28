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
package org.isaacphysics.graphchecker.translation;

import org.isaacphysics.graphchecker.TestHelpers;
import org.isaacphysics.graphchecker.data.Input;
import org.isaacphysics.graphchecker.data.Line;
import org.isaacphysics.graphchecker.dos.Curve;
import org.isaacphysics.graphchecker.dos.GraphAnswer;
import org.isaacphysics.graphchecker.dos.Point;
import org.junit.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.stream.Collectors;

import static org.junit.Assert.assertEquals;

public class AnswerToInputTest {

    private Point toPoint(org.isaacphysics.graphchecker.data.Point p) {
        return new Point(
            p.getX(),
            p.getY()
        );
    }

    private Curve getCurveFromLine(Line line) {
        double minX = line.stream().mapToDouble(org.isaacphysics.graphchecker.data.Point::getX).min().getAsDouble();
        double maxX = line.stream().mapToDouble(org.isaacphysics.graphchecker.data.Point::getX).max().getAsDouble();
        double minY = line.stream().mapToDouble(org.isaacphysics.graphchecker.data.Point::getY).min().getAsDouble();
        double maxY = line.stream().mapToDouble(org.isaacphysics.graphchecker.data.Point::getY).max().getAsDouble();

        return new Curve(
            line.getPoints().stream()
                .map(this::toPoint)
                .collect(Collectors.toList()),
            minX,
            maxX,
            minY,
            maxY,
            null,
            null,
            null,
            Collections.emptyList(),
            Collections.emptyList(),
            0
        );
    }

    @Test
    public void curvesResortedCorrectly() {
        Line right = TestHelpers.lineOf(x -> x, 10, 0);
        Line left = TestHelpers.lineOf(x -> x, 0, -10);

        GraphAnswer answer = new GraphAnswer(1000, 1000, Arrays.asList(
            getCurveFromLine(right),
            getCurveFromLine(left)
        ), Collections.emptyList());

        Collections.reverse(left.getPoints());
        Collections.reverse(right.getPoints());

        Input expected = new Input(Arrays.asList(
            left,
            right
        ));

        Input actual = new AnswerToInput().apply(answer);

        assertEquals(expected, actual);
    }
}